import processing.core.PApplet
import processing.core.PVector

fun main() {
    PApplet.runSketch(arrayOf("Sketch"), Sketch)
}

object Sketch : PApplet() {
    // Internal classes
    data class Edge(val from: Int, val to: Int)

    // Extensions
    operator fun PVector.plus(u: PVector): PVector = PVector.add(this, u)
    operator fun PVector.minus(u: PVector): PVector = PVector.sub(this, u)
    operator fun PVector.unaryMinus(): PVector = PVector(-x, -y, -z)
    private infix fun PVector.lineTo(u: PVector): Unit = line(x, y, u.x, u.y)

    // Constants
    private const val VARIANT = 3106
    private val n = intArrayOf(0, 3, 1, 0, 6)
    private val k = 1.0 - n[3] * 0.01 - n[4] * 0.01 - 0.3
    private val N = n[3] + 10

    // Don't forget to seed RNG!
    init { randomSeed(VARIANT.toLong()) }

    // Graph data
    private var matrix = Array(N) { BooleanArray(N) { 1 <= random(2F) * k } }
    private val unimatrix = Array(N) { i -> BooleanArray(N) { j -> matrix[i][j] || matrix[j][i] } }

    private val points: Array<PVector> =
        (1..<N).map { TWO_PI * it / (N - 1) }.map { PVector(cos(it) * 280, sin(it) * 280) }.toTypedArray() + PVector(
            0F, 0F
        )

    // Global state
    private var directed = true

    // Node routines
    private fun calculateNodePower(n: Int): Int {
        val m = if (directed) matrix else unimatrix

        return m[n].sumOf { if (it) (1).toInt() else 0 } + m.map { it[n] }.sumOf { if (it) (1).toInt() else 0 }
    }

    private fun calculateNodeHalfPowers(n: Int): Pair<Int, Int> {
        val m = if (directed) matrix else unimatrix

        return Pair(m[n].sumOf { if (it) (1).toInt() else 0 }, m.map { it[n] }.sumOf { if (it) (1).toInt() else 0 })
    }

    private fun checkHomogenity(): Boolean {
        val powers = (0..<N).map { calculateNodePower(it) }
        val first = powers[0]

        return powers.all { it == first }
    }

    private fun findTerminalNodes(): List<Int> {
        val nodes = (0..<N).filter { calculateNodePower(it) == 1 }
        return nodes
    }

    private fun findIsolatedNodes(): List<Int> {
        val nodes = (0..<N).filter { calculateNodePower(it) == 0 }
        return nodes
    }

    private fun updateMatrix() {
        val k = 1.0 - n[3] * 0.005 - n[4] * 0.005 - 0.27
        matrix = Array(N) { BooleanArray(N) { 1 <= random(2F) * k } }
    }

    // Printing routines
    private fun displayMatrix() {
        val m = if (directed) matrix else unimatrix

        for (row in m) {
            for (i in row) print("${if (i) '1' else '0'} ")
            println()
        }
    }

    private fun printInfo() {
        directed = false
        println("\nUndirected graph:")
        displayMatrix()
        println()
        directed = true
        println("Directed graph:")
        displayMatrix()

        directed = false
        println("\nUndirected graph node powers:")
        for (i in 0..<N) {
            println("Node $i: ${calculateNodePower(i)}")
        }
        println()
        directed = true
        println("\nDirected graph node powers:")
        for (i in 0..<N) {
            println("Node $i: ${calculateNodePower(i)}")
        }

        println()
        directed = true
        println("\nDirected graph node halfpowers:")
        for (i in 0..<N) {
            val powers = calculateNodeHalfPowers(i)
            println("Node $i: entry - ${powers.first}, exit - ${powers.second}")
        }

        println()
        println(if (checkHomogenity()) "Graph is homogeneous" else "Graph is not homogeneous")

        println()
        println("Isolated nodes: ${findIsolatedNodes()}")
        println("Terminal nodes: ${findTerminalNodes()}")
    }

    // Drawing routines
    override fun settings(): Unit = size(700, 700)

    override fun setup() {
        printInfo()
        directed = true

        updateMatrix()

        directed = true
        println("Updated directed graph:")
        displayMatrix()

        directed = true
        println("\nDirected updated graph node halfpowers:")
        for (i in 0..<N) {
            println("Node $i: ${calculateNodeHalfPowers(i)}")
        }

        windowTitle("Circle Graph")

        colorMode(HSB, 360F, 100F, 100F)
        strokeWeight(2F)
        stroke(255)

        textAlign(CENTER, CENTER)
        textSize(40F)
    }

    override fun draw() {
        background(10)
        translate(width / 2F, height / 2F)

        for ((i, point) in points.withIndex()) {
            for ((j, edge) in matrix[i].withIndex()) {
                if (!edge || i == j) continue

                push()
                run {
                    translate(point.x, point.y)

                    val lineOffset = if (matrix[i][j] && matrix[j][i] && directed) 3F else 0F
                    val end = points[j] - point
                    val dir = -end
                    val offset = dir.copy()
                    offset.setMag(30F)

                    rotate(end.heading())
                    if (directed) arrow(PI, PVector(end.mag() - 30, lineOffset))
                    line(0F, lineOffset, end.mag(), lineOffset)
                }
                pop()
            }
        }

        for ((i, point) in points.withIndex()) {
            push()
            run {
                translate(point.x, point.y)

                if (matrix[i][i]) {
                    noFill()
                    circle(0F, -40.83F, 40F)
                    if (directed) arrow(-1f, PVector(14f, -26.5f))
                }


                fill(100)
                circle(0F, 0F, 60F)
                fill(255)
                text((i + 1F).toInt(), 0F, 0F)
            }
            pop()
        }
    }

    private fun arrow(phi: Float, p: PVector) {
        p lineTo PVector(p.x + 15 * cos(phi + 0.3F), p.y + 15 * sin(phi + 0.3F))
        p lineTo PVector(p.x + 15 * cos(phi - 0.3F), p.y + 15 * sin(phi - 0.3F))
    }

    override fun keyPressed() {
        directed = if (key == ' ') !directed else directed
    }
}
